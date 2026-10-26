# Prompt

## Refinement
This list is updated as soon as the stuff works, only the next few points (TODOs) are listed
1. in the calender view: add swiping support. When swiping left, go to previous month; swiping right to next month.
1. (maybe) use another color for the button outline. The dark-blue already used. And maybe add a light blue background to the button.  
1. (maybe) apply the material 3 expressive design to the app.  
1. feature: add a view where one can flip through/skim through/browse the entries. Starting at the newest one. Should have the look of a diary with animations when browsing from one entry to the other.  
1. try out some different UIs / colors. TODO: maybe with another tool (figma).  


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

## Done
1. Why can't I use Gradle version 10? (latest is 9.8.2)
1. (manually fixed) Bug in the entry streak: instead of the entry streak number, the login-streak number is displayed.
1. The pill with the login streak is not visible on small screens (the entry streak however is visible fine). Have them behave the same way. Login streak pill shall appear after every login
1. make sure all views are scrollable (at least the unlock screen is not scrollable on small screens)
1. add another color to the color palette: something dark blue-ish. Use it for the divider in the statistics card, the horizontal line between the months and the streaks. Also use this color for the streak bars itself, dark blue-ish for the current value, light blue-ish for the record value. 
1. add an 'About' card to the bottom of the main view. Report the copyright of this app, belonging to widmedia. Add a link to widmedia.ch. Also report the used dependencies together with their license names.
