This Maven plugin is designed to process velocity templates.

**********************************************************************************
INSTRUCTIONS:
1. If you want to use short form of plugin and goal (e.g. velocity:process) 
   you should change your maven settings file ($M2_HOME/conf/settings.xml):
   find <pluginGroups> tag and add new child tag 
   <pluginGroup>com.afilin.velocity.maven.plugins</pluginGroup>
   After that you can call this plugin as "mvn velocity:process [parameters]"
2. To build template you should go to the plugin directory 
   "velocity-maven-plugin" and execute command "mvn install"
   After that artifact will be built and put in your local maven repository 
   and you can use it.

**********************************************************************************
USAGE:
mvn velocity:process -Dtemplate.paths="<your_param>" -Dmap.path="<your_param>" \
    -Ddestination.path="<your_param>" -Dzip.skip="<your_param>"
  
  OR

mvn com.afilin.velocity.maven.plugins.velocity:process 
    -Dtemplate.paths="<your_param>" -Dmap.path="<your_param>" \
    -Ddestination.path="<your_param>" -Dzip.skip="<your_param>"

PARAMETERS:
templates.paths - list of path to templates separated by ";" symbol (.vm files)

map.path - path to file with key-value pairs to merge with templates

destination.path - path to folder where plugin should make results of processing
    templates

zip.skip - whether plugin should create zip archive from {destination.path} folder
**********************************************************************************

If you have any questions, comments, corrections or proposals please send me 
letter to anton.y.filin@gmail.com.
