
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author FrankkieNL
 */
public class PoniesOptimizer {

    public PoniesOptimizer() {        
        JOptionPane.showMessageDialog(null, "Copy the 'Ponies' folder from Desktop Ponies, and then select the copied folder.\nThis program will remove all useless files to reduce space.");
        File folder = getFolder();
        String[] ponyFolders = folder.list();
        for (String ponyFolder : ponyFolders) {
            String tempFolderName = folder.getAbsolutePath() + File.separator + ponyFolder;
            File pFolder = new File(tempFolderName);
            String[] pFiles = pFolder.list();
            if (pFiles == null){
                continue;
            }
            System.out.println("Processing: " + ponyFolder);
            for (String pFile : pFiles) {
                //remove sounds (not used by android live wallpaper)
                if (pFile.endsWith(".mp3") || pFile.endsWith(".wav")) {
                    File delFile = new File(folder.getAbsolutePath() + File.separator + ponyFolder + File.separator + pFile);
                    System.out.println("Delete: " + delFile.getAbsolutePath());
                    delFile.delete();
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Done! no paste the resulting folder in the Android Live Wallpaper");
    }

    public static File getFolder() {
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //In response to a button click:
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File folder = fc.getSelectedFile();
            return folder;
        } else {
            System.err.println("Please Select Folder !");
            return null;
        }
    }

    public static void main(String[] args) {
        new PoniesOptimizer();
    }
}
