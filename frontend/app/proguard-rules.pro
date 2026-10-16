# R8 rules for the release build (KAN-41).
# Libraries (Compose, Navigation, CameraX, ML Kit, kotlinx.serialization) ship their
# own consumer rules; only app-specific needs are listed here.

# --- Type-safe navigation routes ---------------------------------------------
# Navigation looks routes up through their generated kotlinx.serialization
# serializers. If R8 renames or strips them the app builds fine but crashes
# the first time it navigates, so keep every route and its serializer.
-keep class com.example.frontend.navigation.Screen { *; }
-keep class com.example.frontend.navigation.Screen$* { *; }
-keepclassmembers class com.example.frontend.navigation.Screen$** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Other @Serializable app classes -------------------------------------------
# Vote flow state is saved to SavedStateHandle as JSON (VoteFlowViewModel), and
# Ballot is a route argument. Keep their serializers and enum names stable so
# restored state still decodes after process death.
-keepclassmembers @kotlinx.serialization.Serializable class com.example.frontend.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.frontend.**$$serializer { *; }
-keepclassmembers enum com.example.frontend.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- ML Kit face detection -----------------------------------------------------
# ML Kit wires itself together through a reflective component registry. Its own
# rules are not enough under R8 full mode: FaceDetection.getClient() came back
# null in the release build and the face-check screens crashed on open.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-keep class com.google.firebase.components.** { *; }

# Readable crash reports: keep file and line numbers, hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
