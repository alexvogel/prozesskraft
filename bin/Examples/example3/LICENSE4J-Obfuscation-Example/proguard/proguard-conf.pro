#
# SAMPLE PROGUARD CONFIGURATION FILE
#

# LICENSE4J-Obfuscation-Example.jar file is the application jar file
# LICENSE4J-Runtime-Library.jar library file is given to injars so it is
# obfuscated. If runtime library is specified in librarjars it is NOT obfuscated.
# outjars file name ends with "temp" because final modifications will be made 
# after obfuscation by ant task, finally file name will be "LICENSE4J-Obfuscation-Example.jar".
-injars '..\dist\LICENSE4J-Obfuscation-Example.jar';'..\lib\LICENSE4J-Runtime-Library.jar'
-outjars '..\dist\obfuscated\LICENSE4J-Obfuscation-Example-temp.jar'


# Define Java path. If JAVA_HOME system variable is defined, <java.home> can be used.
-libraryjars 'C:\Program Files\Java\jre7\lib\rt.jar'
-libraryjars 'C:\Program Files\Java\jre7\lib\jce.jar'


# Verbose output, there will be a warning about metafile, ignore it.
-verbose


# Create a mapping file in specified folder.
-printmapping '..\dist\obfuscated\obfuscated-mapping.txt'


-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod


# Finally keep main class.
-keep public class com.example.MainJFrame {
    public static void main(java.lang.String[]);
}
