/*
 * Created by JFormDesigner on Sun Aug 28 22:38:11 MSK 2022
 */

package unluac.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import unluac.Configuration;
import unluac.Main;
import unluac.entity.AlbFile;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author unknown
 */
public class Gui extends JFrame {

    private final String[] argsArray;
    private final byte[] luaHeaderBytes = {27, 76, 117, 97};

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel mainPanel;
    private JLabel settingsLabel;
    private JLabel dragDropLabel;
    private JComboBox<String> argsComboBox;
    private JCheckBox overwriteCheckBox;
    private JCheckBox autoCloseCheckBox;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private List<AlbFile> albFileList;

    public Gui(String[] argsArray) {
        this.argsArray = argsArray;
        this.albFileList = new ArrayList<>();
        FlatDarkLaf.setup();
        initComponents();
        initFileDragDrop();
        initArgsComboBox();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        mainPanel = new JPanel();
        settingsLabel = new JLabel();
        dragDropLabel = new JLabel();
        argsComboBox = new JComboBox();
        overwriteCheckBox = new JCheckBox();
        autoCloseCheckBox = new JCheckBox();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== mainPanel ========
        {
            mainPanel.setLayout(null);

            //---- settingsLabel ----
            settingsLabel.setBorder(new LineBorder(Color.black, 1, true));
            mainPanel.add(settingsLabel);
            settingsLabel.setBounds(360, 10, 130, 300);

            //---- dragDropLabel ----
            dragDropLabel.setBorder(new LineBorder(Color.black, 1, true));
            dragDropLabel.setText("Drag & Drop");
            dragDropLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(dragDropLabel);
            dragDropLabel.setBounds(10, 10, 340, 300);
            mainPanel.add(argsComboBox);
            argsComboBox.setBounds(370, 20, 110, 35);

            //---- overwriteCheckBox ----
            overwriteCheckBox.setText("Overwrite file(s)");
            mainPanel.add(overwriteCheckBox);
            overwriteCheckBox.setBounds(370, 65, 110, overwriteCheckBox.getPreferredSize().height);

            //---- autoCloseCheckBox ----
            autoCloseCheckBox.setText("Auto close app");
            autoCloseCheckBox.setSelected(true);
            mainPanel.add(autoCloseCheckBox);
            autoCloseCheckBox.setBounds(370, 100, 130, autoCloseCheckBox.getPreferredSize().height);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for (int i = 0; i < mainPanel.getComponentCount(); i++) {
                    Rectangle bounds = mainPanel.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = mainPanel.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                mainPanel.setMinimumSize(preferredSize);
                mainPanel.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(mainPanel);
        mainPanel.setBounds(0, 0, 500, 320);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for (int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void initFileDragDrop() {
        TransferHandler transferHandler = new TransferHandler() {
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    List<File> dragDropFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    analyseDragDropFiles(dragDropFiles);
                    analyseAlbFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                return true;
            }
        };
        dragDropLabel.setTransferHandler(transferHandler);
    }

    private void analyseAlbFiles() {
        if (0 < albFileList.size()) {
            changeEnable(false);
            Configuration config = new Configuration();
            config.autoClose = autoCloseCheckBox.isSelected();
            Main.analyseAlbFiles(albFileList, config);
            changeEnable(true);
        }
    }

    private void changeEnable(boolean isEnable) {
        dragDropLabel.setEnabled(isEnable);
        settingsLabel.setEnabled(isEnable);
        argsComboBox.setEnabled(isEnable);
        overwriteCheckBox.setEnabled(isEnable);
        autoCloseCheckBox.setEnabled(isEnable);
    }

    private void analyseDragDropFiles(List<File> dragDropFiles) throws IOException {
        for (File dragDropFile : dragDropFiles) {
            if (dragDropFile.isFile() && isValidHeader(dragDropFile)) {
                addOnlyCompiledFile(dragDropFile);
                continue;
            }
            if (dragDropFile.isDirectory()) {
                getFileFromDirectory(dragDropFile);
            }
        }
    }

    private void getFileFromDirectory(File directory) throws IOException {
        File[] inDirectoryFiles = Objects.requireNonNull(directory.listFiles());
        if (0 < inDirectoryFiles.length) {
            for (File inDirectoryFile : inDirectoryFiles) {
                if (inDirectoryFile.isFile() && isValidHeader(inDirectoryFile)) {
                    addOnlyCompiledFile(inDirectoryFile);
                    continue;
                }
                if (inDirectoryFile.isDirectory()) {
                    getFileFromDirectory(inDirectoryFile);
                }
            }
        }
    }

    private boolean isValidHeader(File file) throws IOException {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            if (4 <= randomAccessFile.length()) {
                byte[] headerBytes = new byte[4];
                randomAccessFile.read(headerBytes);
                return Arrays.equals(luaHeaderBytes, headerBytes);
            }
            return false;
        }
    }

    private void addOnlyCompiledFile(File file) {
        AlbFile albFile = new AlbFile(file, String.valueOf(argsComboBox.getSelectedItem()), overwriteCheckBox.isSelected());
        albFileList.add(albFile);
    }

    private void initArgsComboBox() {
        this.argsComboBox.setModel(new DefaultComboBoxModel<>(argsArray));
    }

}
