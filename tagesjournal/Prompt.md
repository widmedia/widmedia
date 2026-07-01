# Prompt

## Refinement
This list is updated as soon as the stuff works, only the next few points (TODOs) are listed 
1. (maybe) use another color for the button outline. Something dark-blueish.  
1. (maybe) apply the material 3 expressive design to the app
1. (maybe) enable previews for the UI screens
1. fix all the build warnings.  
1. try out some different UIs / colors. TODO: maybe with another tool (figma)
1. have the lock symbol more prominent (TODO: decide how)


## Webpage
1. Manually: change the texts. 


## App store
1. have tablet screenshots (tablet with fingerprint?)  


<br /><br /><br />

---
## Won't do
- ignore the PiP warning. Will not implement PiP behavior, there is no meaningful usecase for it.  
- Does not work, probably Android/Samsung specific: 
  on the Sperrscreen: always have the 3 standard android buttons visible (back/home/overview)
- Does not work due to sqlcipher usage:  
  I get this warning when publishing it: 
  "This App Bundle contains native code, and you've not uploaded debug symbols. We recommend that you upload a symbol file to make your crashes and ANRs easier to analyse and debug." and when I analyze my build, I don't have the BUNDLE-METADATA/com.android.tools.build.debugsymbols folder. NDK version 27.3.13750724 is installed.