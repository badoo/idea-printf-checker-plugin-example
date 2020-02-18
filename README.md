Badoo Example plugin (printf-like functions)
============================================

IDE Setup 
---------
You will need IDEA Community Edition or IDEA Ultimate.
PhpStorm can't have `Plugin DevKit` installed

1. Checkout source, i.e. ```$ git clone git@git.mlan:intellij-idea-badoo-components.git```
2. Install `Gradle` plugin in IDEA
3. Open project
4. Idea will suggest to import Gradle configuration, agree and chose `Use gradle 'wrapper' task configuration`
5. View -> Tool windows -> Gradle 


Build From IDE
--------------
View -> Tool windows -> Gradle -> Tasks -> intellij -> buildPlugin 

Build From Command line
-----------------------
```
$ ./gradlew --info buildPlugin
```

The plugin will be placed in **build/distributions/**

Run tests from command like
-----------------------
```
$ ./gradlew test
```

Test result report will be placed in **build/reports/tests/test/index.html**

Debug
-----

1. Run PhpStorm
2. Help -> Edit Custom VM Options...
3. Add line: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
4. Install new version of the plugin & restart PhpStorm
5. In Idea Community, go to Run -> Attach to Process...
   If everything is right, in the list you will find the process of PhpStorm
6. Set breakpoint and enjoy the debug.
