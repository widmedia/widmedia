# Prompt

## Refinement
This list is updated as soon as the stuff works, only the next few points (TODOs) are listed 
1. enable previews for the UI screens   
1. (maybe) use another color for the button outline. Something dark-blueish. And add a light blue background to the button
1. (maybe) apply the material 3 expressive design to the app
1. fix all the build warnings.  
1. add a view where one can flip through/skim through/browse the entries. Starting at the newest one. Should have the look of a diary with animations when browsing from one entry to the other.  
1. try out some different UIs / colors. TODO: maybe with another tool (figma)



## Webpage
1. 


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