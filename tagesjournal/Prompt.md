# Prompt

## Refinement
This list is updated as soon as the stuff works, only the next few points (TODOs) are listed 
1. re-think the picture-in-picture mode. Not really a use case, app should close or go into the background.  

1. Move the link to the settings page into the card where 'alle Einträge anzeigen' is shown. Give this card a title.  
1. rename the settings page to import/export and change the symbol accordingly.  
1. visually rework the import/export page, make it more clearly arranged.  
1. In the @AlleEintraegeScreen.kt: add an option to delete all entries. Add a warning card before actually deleting all entries.  
1. clean up the code. Check for newer stable versions of dependencies and in general reduce the number of imports and dependencies.  
1. try out some different UIs / colors. TODO: maybe with another tool (figma)
1. have the lock symbol more prominent (TODO: decide how)


## Webpage
1. Manually: change the texts. 




## GIT re-org
1. test on second workspace


## App store
1. have tablet screenshots (tablet with fingerprint?)  


<br /><br /><br />

---
## Won't do
- Does not work, probably Android/Samsung specific: 
  on the Sperrscreen: always have the 3 standard android buttons visible (back/home/overview)
- Does not work due to sqlcipher usage:  
  I get this warning when publishing it: 
  "This App Bundle contains native code, and you've not uploaded debug symbols. We recommend that you upload a symbol file to make your crashes and ANRs easier to analyse and debug." and when I analyze my build, I don't have the BUNDLE-METADATA/com.android.tools.build.debugsymbols folder. NDK version 27.3.13750724 is installed.