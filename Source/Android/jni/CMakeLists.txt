add_library(main SHARED
  Cheats/ARCheat.cpp
  Cheats/Cheats.h
  Cheats/GeckoCheat.cpp
  Cheats/PatchCheat.cpp
  Config/NativeConfig.cpp
  Config/PostProcessing.cpp
  GameList/GameFile.cpp
  GameList/GameFile.h
  GameList/GameFileCache.cpp
  IniFile.cpp
  MainAndroid.cpp
  RiivolutionPatches.cpp
  WiiUtils.cpp
)

target_link_libraries(main
PRIVATE
  androidcommon
  common
  core
  uicommon
)

target_link_libraries(main
PRIVATE
  android
  log
  "-Wl,--no-warn-mismatch"
  "-Wl,--whole-archive"
  "-Wl,--no-whole-archive"
)

file(MAKE_DIRECTORY ${CMAKE_SOURCE_DIR}/Source/Android/app/src/main/assets/)
file(REMOVE_RECURSE ${CMAKE_SOURCE_DIR}/Source/Android/app/src/main/assets/Sys/)
file(COPY ${CMAKE_SOURCE_DIR}/Data/Sys DESTINATION ${CMAKE_SOURCE_DIR}/Source/Android/app/src/main/assets/)

# Delete folders that aren't used by the Android version of Dolphin
file(REMOVE_RECURSE ${CMAKE_SOURCE_DIR}/Source/Android/app/src/main/assets/Sys/Resources/)
file(REMOVE_RECURSE ${CMAKE_SOURCE_DIR}/Source/Android/app/src/main/assets/Sys/Themes/)

set(CPACK_PACKAGE_EXECUTABLES ${CPACK_PACKAGE_EXECUTABLES} main)

add_subdirectory(AndroidCommon)
