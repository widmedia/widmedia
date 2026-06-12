# Prompt

## Refinement
This list is updated as soon as the stuff works, only the next few points (TODOs) are listed 
1. Tutorial: delete the newly created entry at the end of the tutorial. Do this also when the skip-tutorial button is pressed (catch the case where there is no entry to be deleted)
1. Tutorial: add a 1/6 counter to the title.  
1. Entry page: there is a green button on green background. Add a card around it.  
1. re-think the picture-in-picture mode. Not really a use case, app should close or go into the background.  

1. rework the settings page, make it more clearly arranged. Maybe: change name and symbol to import/export. Maybe have the link to the import/export at the page where all entries are visible and thus no special symbol anymore on the main page?
1. clean up the code. Check for newer stable versions of dependencies and in general reduce the number of imports and dependencies.  
1. try out some different UIs / colors. TODO: maybe with another tool (figma)
1. have the lock symbol more prominent (TODO: decide how)


## Webpage
1. Modify the page on https://widmedia.ch/index.html. Change the block about the TagesWert app. Base the design on the picture available at https://widmedia.ch/img/StoreIcon2.png. For the block integrate the screenshot which can be found on https://widmedia.ch/img/blabla.png. Use the Raleway font for the title (the font is available on https://widmedia.ch/schwimmmesser/fonts/raleway-v12-latin-regular.woff).  Make sure to use relative paths in the html file. Do not change the other blocks of the html page.

Manually: change the texts. 



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