# Linea Launcher - keep serialization models
-keepattributes *Annotation*
-keep,includedescriptorclasses class com.linea.launcher.**$$serializer { *; }
-keepclassmembers class com.linea.launcher.** { *** Companion; }
