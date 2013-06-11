package com.afilin.velocity.maven.plugins;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Process Velocity templates
 *
 * @goal process
 * @requiresProject false
 */
public class VelocityMojo extends AbstractMojo {

    private static final String TEMPLATE_PATH_DELIMITER = ";";

    private static final String VELOCITY_PATH_PROPERTY = "file.resource.loader.path";
    private static final String VELOCITY_PATH_DELIMITER = ",";
    private static final String VELOCITY_TEMPLATE_EXTENSION = ".vm";

    private static final String TARGET_FILE_EXTENSION = ".txt";
    private static final String ARCHIVE_EXTENSION = ".zip";

    private static final Integer BUFFER_SIZE = 2048;
    /**
     * The list of template paths separated by ';' symbol
     *
     * @parameter expression="${template.paths}"
     * @required
     */
    private String templatePaths;

    /**
     * The path of file with template map key->value
     *
     * @parameter expression="${map.path}"
     * @required
     * @type java.io.File
     */
    private File keyValueMap;

    /**
     * The folder path for processed templates
     *
     * @parameter expression="${destination.path}"
     * @required
     * @type java.io.File
     */
    private File destinationFolder;

    /**
     * Whether the program should skip packing destination folder in zip-archive
     *
     * @parameter expression="${zip.skip}"
     * @required
     * @type java.lang.Boolean
     */
    private Boolean skipZip;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties propertiesValue = null;
        try {
            propertiesValue = initializeProperties(keyValueMap);
        } catch (IOException e) {
            getLog().error("There is the problem when trying to load key-value pairs. ", e);
            return;
        }
        VelocityContext context = new VelocityContext(propertiesValue);
        String[] templatePathsArray = templatePaths.split(TEMPLATE_PATH_DELIMITER);
        Velocity.setProperty(VELOCITY_PATH_PROPERTY, makeVelocityPath(templatePathsArray));
        Velocity.init();

        if (!destinationFolder.exists()) {
            getLog().error("The target folder " + destinationFolder.getPath() + " isn't exist. Please create it and re-run the goal.");
            return;
        }
        for (String templatePath : templatePathsArray) {
            File templateFile = new File(templatePath);
            if (templateFile.exists()) {
                Template template = Velocity.getTemplate(templateFile.getName());
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(
                                            new File(
                                                    destinationFolder +
                                                            (!destinationFolder.getPath().endsWith(File.separator) ? File.separator : "") +
                                                            templateFile.getName().replace(VELOCITY_TEMPLATE_EXTENSION, TARGET_FILE_EXTENSION)
                                            )
                                    )
                            )
                    );
                    template.merge(context, writer);
                    writer.flush();
                } catch (IOException e) {
                    getLog().error("There is problem when trying to write processed template " + templateFile.getPath() + ";" + e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            getLog().warn("Plugin can't close stream.", e);
                        }
                    }
                }
            } else {
                getLog().warn("The file " + templateFile.getPath() + " is not exist, skip it.");
            }
        }
        if (!skipZip) {
            makeZipFromFolder(destinationFolder, new File(destinationFolder.getParent() + File.separator + destinationFolder.getName() + ARCHIVE_EXTENSION));
        }
    }

    private String makeVelocityPath(String[] templatePaths) {
        StringBuilder resultPath = new StringBuilder();
        for (String templatePath : templatePaths) {
            resultPath.append(new File(templatePath).getParent());
            resultPath.append(VELOCITY_PATH_DELIMITER);
        }
        resultPath.append(".");
        return resultPath.toString();
    }

    private Properties initializeProperties(File propertiesFile) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        props.load(fis);
        fis.close();
        return props;
    }

    private void makeZipFromFolder(File sourceFolder, File destinationZip) {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destinationZip)));
            zip(sourceFolder, "", zipOutputStream);
            zipOutputStream.flush();
        } catch (FileNotFoundException e) {
            getLog().warn("The file " + destinationZip.getPath() + " isn't exist. ", e);
        } catch (IOException e) {
            getLog().warn("There are some problems when trying to create zip-archive. ", e);
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                getLog().warn("The plugin can't close stream for creating zip.", e);
            }
        }
    }

    /**
     * This method bypasses folder recursively and create zip from it.
     */
    private void zip(File file, String path, ZipOutputStream zipOutputStream) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            // to prevent losing the empty directories we need to process them explicitly
            if (ArrayUtils.isEmpty(children)) {
                zipOutputStream.putNextEntry(new ZipEntry(path + File.separator));
                return;
            }
            for (File child : file.listFiles()) {
                zip(child, path + (path.isEmpty() ? "" : File.separator) + child.getName(), zipOutputStream);
            }
        } else {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file.getPath()));
            zipOutputStream.putNextEntry(new ZipEntry(path));
            int count;
            byte[] dataBuffer = new byte[BUFFER_SIZE];
            while ((count = inputStream.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                zipOutputStream.write(dataBuffer, 0, count);
            }
            inputStream.close();
        }
    }
}