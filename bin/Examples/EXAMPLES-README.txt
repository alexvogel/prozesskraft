EXAMPLES README

example1 folder includes source code for validation of each license type.

example2 folder includes a Netbeans project for complete application with support for
validation of each license type. Application also save license information and
use at startup like a real product. 

example3 folder includes a Netbeans project to demonstrate License4J Runtime Library
obfuscation with with any application. You can find ant task, proguard config file 
and proguard itself.

example4 folder includes a Netbeans project. It demonstrates license key with activation
feature usage. It also includes trial license usage.

example5 folder includes an example to validate and activate license key/text. You
can directly use that example in your application to easily add licensing support.

example6 is a very small example application, only includes a JFrame. It includes jar file from
example5 to demonstrate how to integrate licensing in your software easily. Source file has comments,
and it only call methods from example5. It also includes hard-coded trial license key usage.

example7 is a floating license text validation example. Example use new validateFloatingLicenseText
method and FloatingLicenseTimerTaskHandler. This timer handler has license object so it is easy
to update license information dialog or call other methods depending on license validation status.

example8 is a online license key floating over internet validation example.
Example use new validateOnlineLicenseKey method and FloatingLicenseTimerTaskHandler. This
timer handler has license object so it is easy to update license information dialog or call
other methods depending on license validation status.

example9 includes two JFrame example applications. It validates hardcoded license key
and check for update notifications and license messages. If found it displays an option pane.

example10 is another quick start example. All packages includes two Java source
files. MyProductMainJFrame.java file is our example very simple Java application.
Read source and just copy the code commented as "COPY THIS LINE" and
"COPY THIS METHOD" to your main Java file. Then copy the second file which
includes all licensing methods and a simple licensing jdialog. Open jdialog
source, and change only variables commented as "REQUIRED". THAT IS ALL.

example11 folder includes a sample web application using license4j for licensing.
There is a simple license validation helper class which is used in servlets. Startup
servlet validates license on context initialization. License validation can also be
performed in any other servlet.
