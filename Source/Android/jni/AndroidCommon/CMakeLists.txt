add_library(androidcommon STATIC
  AndroidCommon.cpp
  AndroidCommon.h
  IDCache.cpp
  IDCache.h
  AndroidHotkey.cpp
  AndroidHotkey.h
  AndroidTheme.cpp
  AndroidTheme.h
)

target_link_libraries(androidcommon
PRIVATE
  android
  common
  log
  "-Wl,--no-warn-mismatch"
  "-Wl,--whole-archive"
  "-Wl,--no-whole-archive"
)
