# Being able to get sensible stack traces from users is more important
# than the space savings obfuscation could give us
-dontobfuscate
-dontwarn javax.annotation.Nullable
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider