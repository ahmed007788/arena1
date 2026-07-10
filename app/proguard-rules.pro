# Arena AI ProGuard Rules
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep class com.arena.ai.domain.model.** { *; }
-keepattributes Signature, Exceptions
-keep class * extends androidx.room.RoomDatabase
