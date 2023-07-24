-printmapping out.map
-flattenpackagehierarchy ''

# Warns
-dontwarn org.apache.logging.**
-dontwarn net.dv8tion.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn com.neovisionaries.ws.client.**

# Gson
-keepattributes Signature
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Classes
-keep class com.esotericsoftware.** { *; }
-keep class com.sun.** { *; }
-keep class org.apache.logging.log4j.** { *; }

-keep class com.photon.informations.ObjectInfos { <fields>; }
-keep class com.photon.network.objects.** { <fields>; }
-keep class com.photon.network.NetworkDirectories$* { <fields>; }

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}
-keepclasseswithmembers public class com.photon.network.NetworkEngine {
    public static void main(java.lang.String[]);
}
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}
-keepclasseswithmembernames class * { native <methods>; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Disable certain proguard optimizations which remove stackframes (same as Android defaults)
-optimizations !method/inlining/*

# Libs
-libraryjars '<java.home>\jmods\java.base.jmod'
-libraryjars '<java.home>\jmods\java.compiler.jmod'
-libraryjars '<java.home>\jmods\java.datatransfer.jmod'
-libraryjars '<java.home>\jmods\java.desktop.jmod'
-libraryjars '<java.home>\jmods\java.instrument.jmod'
-libraryjars '<java.home>\jmods\java.logging.jmod'
-libraryjars '<java.home>\jmods\java.management.jmod'
-libraryjars '<java.home>\jmods\java.management.rmi.jmod'
-libraryjars '<java.home>\jmods\java.naming.jmod'
-libraryjars '<java.home>\jmods\java.net.http.jmod'
-libraryjars '<java.home>\jmods\java.prefs.jmod'
-libraryjars '<java.home>\jmods\java.rmi.jmod'
-libraryjars '<java.home>\jmods\java.scripting.jmod'
-libraryjars '<java.home>\jmods\java.se.jmod'
-libraryjars '<java.home>\jmods\java.security.jgss.jmod'
-libraryjars '<java.home>\jmods\java.security.sasl.jmod'
-libraryjars '<java.home>\jmods\java.smartcardio.jmod'
-libraryjars '<java.home>\jmods\java.sql.jmod'
-libraryjars '<java.home>\jmods\java.sql.rowset.jmod'
-libraryjars '<java.home>\jmods\java.transaction.xa.jmod'
-libraryjars '<java.home>\jmods\java.xml.crypto.jmod'
-libraryjars '<java.home>\jmods\java.xml.jmod'
-libraryjars '<java.home>\jmods\jdk.accessibility.jmod'
-libraryjars '<java.home>\jmods\jdk.attach.jmod'
-libraryjars '<java.home>\jmods\jdk.charsets.jmod'
-libraryjars '<java.home>\jmods\jdk.compiler.jmod'
-libraryjars '<java.home>\jmods\jdk.crypto.cryptoki.jmod'
-libraryjars '<java.home>\jmods\jdk.crypto.ec.jmod'
-libraryjars '<java.home>\jmods\jdk.crypto.mscapi.jmod'
-libraryjars '<java.home>\jmods\jdk.dynalink.jmod'
-libraryjars '<java.home>\jmods\jdk.editpad.jmod'
-libraryjars '<java.home>\jmods\jdk.hotspot.agent.jmod'
-libraryjars '<java.home>\jmods\jdk.httpserver.jmod'
-libraryjars '<java.home>\jmods\jdk.incubator.foreign.jmod'
-libraryjars '<java.home>\jmods\jdk.incubator.vector.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.ed.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.jvmstat.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.le.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.opt.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.vm.ci.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.vm.compiler.jmod'
-libraryjars '<java.home>\jmods\jdk.internal.vm.compiler.management.jmod'
-libraryjars '<java.home>\jmods\jdk.jartool.jmod'
-libraryjars '<java.home>\jmods\jdk.javadoc.jmod'
-libraryjars '<java.home>\jmods\jdk.jcmd.jmod'
-libraryjars '<java.home>\jmods\jdk.jconsole.jmod'
-libraryjars '<java.home>\jmods\jdk.jdeps.jmod'
-libraryjars '<java.home>\jmods\jdk.jdi.jmod'
-libraryjars '<java.home>\jmods\jdk.jdwp.agent.jmod'
-libraryjars '<java.home>\jmods\jdk.jfr.jmod'
-libraryjars '<java.home>\jmods\jdk.jlink.jmod'
-libraryjars '<java.home>\jmods\jdk.jpackage.jmod'
-libraryjars '<java.home>\jmods\jdk.jshell.jmod'
-libraryjars '<java.home>\jmods\jdk.jsobject.jmod'
-libraryjars '<java.home>\jmods\jdk.jstatd.jmod'
-libraryjars '<java.home>\jmods\jdk.localedata.jmod'
-libraryjars '<java.home>\jmods\jdk.management.agent.jmod'
-libraryjars '<java.home>\jmods\jdk.management.jfr.jmod'
-libraryjars '<java.home>\jmods\jdk.management.jmod'
-libraryjars '<java.home>\jmods\jdk.naming.dns.jmod'
-libraryjars '<java.home>\jmods\jdk.naming.rmi.jmod'
-libraryjars '<java.home>\jmods\jdk.net.jmod'
-libraryjars '<java.home>\jmods\jdk.nio.mapmode.jmod'
-libraryjars '<java.home>\jmods\jdk.random.jmod'
-libraryjars '<java.home>\jmods\jdk.sctp.jmod'
-libraryjars '<java.home>\jmods\jdk.security.auth.jmod'
-libraryjars '<java.home>\jmods\jdk.security.jgss.jmod'
-libraryjars '<java.home>\jmods\jdk.unsupported.desktop.jmod'
-libraryjars '<java.home>\jmods\jdk.unsupported.jmod'
-libraryjars '<java.home>\jmods\jdk.xml.dom.jmod'
-libraryjars '<java.home>\jmods\jdk.zipfs.jmod'