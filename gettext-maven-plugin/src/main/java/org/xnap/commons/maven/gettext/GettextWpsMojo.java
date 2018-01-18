/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xnap.commons.maven.gettext;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Invokes xgettext to extract messages from source code and store them in the keys.pot file.
 */
@Mojo(name = "gettext-wps", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GettextWpsMojo extends AbstractGettextMojo {
	
    /**
     * The encoding of the source Java files. utf-8 is a superset of ascii.
     */
	@Parameter(defaultValue = "utf-8", required = true)
	protected String encoding;
	
    /**
     * The keywords the xgettext parser will look for to extract messages. The default value works with the Gettext Commons library.
     */
	@Parameter(defaultValue = "-ktrc:1c,2 -ktrnc:1c,2,3 -ktr -kmarktr -ktrn:1,2 -k", required = true)
    protected String keywords;

	/**
	 * The xgettext command.
	 */
	@Parameter(defaultValue = "xgettext", required = true)
	protected String xgettextCmd;

	/**
	 * Parse and add the Wps Groovy annotation text if true, ignore them otherwise.
	 */
	@Parameter(defaultValue = "true")
	protected boolean parseAnnotations;

	/**
	 * An optional set of source files that should be parsed with xgettext.
	 * <pre>
	 * <includes>
	 *    <include>** /*.java</include>
	 * </includes>
	 * </pre>
	 */
	@Parameter(defaultValue = "**/*.java, **/*.groovy")
	protected String[] includes;
    
    /**
     * An optional set of extra source files that should also be parsed with xgettext.
     * <pre>
     * <extraSourceFiles>
     *   <directory>${basedir}</directory>
     *   <includes>
     *      <include>** /*.jsp</include>
     *    </includes>
     *    <excludes>
     *      <exclude>** /*.txt</exclude>
     *    </excludes>
     * </extraSourceFiles>
     * </pre>
     */
	@Parameter
	protected FileSet extraSourceFiles;

	public void execute() throws MojoExecutionException {
		getLog().info("Invoking xgettext for Java files in '" + sourceDirectory.getAbsolutePath() + "'.");

		Commandline cl = new Commandline();
		cl.setExecutable(xgettextCmd);
    	cl.createArg().setValue("--from-code=" + encoding);
    	cl.createArg().setValue("--output=" + new File(poDirectory, keysFile).getAbsolutePath());
    	cl.createArg().setValue("--language=Java");
    	cl.createArg().setLine(keywords);
    	cl.setWorkingDirectory(sourceDirectory.getAbsolutePath());

    	DirectoryScanner ds = new DirectoryScanner();
    	ds.setBasedir(sourceDirectory);
    	ds.setIncludes(includes);
    	ds.scan();
        String[] files = ds.getIncludedFiles();
        List<String> fileNameList = Collections.emptyList();
        if (extraSourceFiles != null && extraSourceFiles.getDirectory() != null) {
        	try {
        		fileNameList = FileUtils.getFileNames(new File(extraSourceFiles.getDirectory()), 
        				StringUtils.join(extraSourceFiles.getIncludes().iterator(), ","), 
        				StringUtils.join(extraSourceFiles.getExcludes().iterator(), ","), false);
        	} catch (IOException e) {
        		throw new MojoExecutionException("error finding extra source files", e);
        	}
        }

    	File file = createListFile(files, fileNameList);
    	if (file != null) {
    	    cl.createArg().setValue("--files-from=" + file.getAbsolutePath());
    	} else {
			for (String f : files) {
				getLog().info(getAbsolutePath(f));
				cl.createArg().setValue(getAbsolutePath(f));
			}
    	}

    	getLog().debug("Executing: " + cl.toString());
    	StreamConsumer out = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.INFO);
    	StreamConsumer err = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.WARN);
    	try {
    	    CommandLineUtils.executeCommandLine(cl, out, err);
    	} catch (CommandLineException e) {
    	    getLog().error("Could not execute " + xgettextCmd + ".", e);
    	}

    	if(parseAnnotations){
    		executeOnAnnotations();
		}
    }

    private void executeOnAnnotations() throws MojoExecutionException {
		getLog().info("Invoking xgettext on groovy wps annotation in '" + sourceDirectory.getAbsolutePath() + "'.");

		DirectoryScanner ds = new DirectoryScanner();
		ds.setBasedir(sourceDirectory);
		ds.setIncludes(new String[]{"**/*.groovy"});
		ds.scan();
		String[] files = ds.getIncludedFiles();
		try {
			for(String fileName : files){
				File file = new File(sourceDirectory+File.separator+fileName);
				File textFile = File.createTempFile(fileName, null, file.getParentFile());
				textFile.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
					String line;
					String text = "";
					int openPar = 0;
					int closedPar = 0;
					while ((line = br.readLine()) != null) {
						if(line.trim().indexOf("@") == 0){
							text = "";
							openPar = 0;
							closedPar = 0;
						}
						if (openPar != 0 && openPar == closedPar) {
							openPar = 0;
							Pattern p = Pattern.compile("(title|description|keywords|names).?.?.?\"([^\"]*)\"");
							Matcher m = p.matcher(text);
							while (m.find()) {
								writer.write("i18n.tr(\""+m.group(2)+"\")");
								writer.newLine();
							}
						} else {
							text += line;
							openPar += line.length() - line.replace("(", "").length();
							closedPar += line.length() - line.replace(")", "").length();
						}
					}
				}finally {
					try {
						writer.close();
					} catch (IOException e) {
						getLog().error("Could not close stream.", e);
					}
				}

				Commandline cl = new Commandline();
				cl.setExecutable(xgettextCmd);
				cl.createArg().setValue(textFile.getAbsolutePath().replaceAll(sourceDirectory.getAbsolutePath()+File.separator, ""));
				cl.createArg().setValue("--from-code=" + encoding);
				cl.createArg().setValue("--output=" + new File(poDirectory, keysFile).getAbsolutePath());
				cl.createArg().setValue("--language=Java");
				cl.createArg().setValue("--join-existing");
				cl.createArg().setLine(keywords);
				cl.setWorkingDirectory(sourceDirectory.getAbsolutePath());

				getLog().debug("Executing: " + cl.toString());
				StreamConsumer out = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.INFO);
				StreamConsumer err = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.WARN);
				try {
					CommandLineUtils.executeCommandLine(cl, out, err);
				} catch (CommandLineException e) {
					getLog().error("Could not execute " + xgettextCmd + ".", e);
				}
			}
			//parse file
		} catch (IOException e) {
			getLog().error("Could not create the text file.", e);
			return;
		}
	}

    private File createListFile(String[] files, List<String> fileList) {
        try {
            File listFile = File.createTempFile("maven", null);
            listFile.deleteOnExit();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(listFile));
            try {
				for (String file : files) {
					writer.write(toUnixPath(file));
					writer.newLine();
				}
				for (String file : fileList) {
					writer.write(toUnixPath(file));
					writer.newLine();
				}
            } finally {
                writer.close();
            }
            
            return listFile;
        } catch (IOException e) {
            getLog().error("Could not create list file.", e);
            return null;
        }
    }
    
    private String getAbsolutePath(String path) {
    	return sourceDirectory.getAbsolutePath() + File.separator + path;
    }
    
    private String toUnixPath(String path) {
        if (File.separatorChar != '/') {
        	return path.replace(File.separatorChar, '/');
        }
        return path;
    }
    
}
