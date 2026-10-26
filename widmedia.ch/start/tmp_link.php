<?php declare(strict_types=1);
  // this file does the redirection and increases the link counter by one
  require_once 'functions.php';

  function printMsgAndDie (string $heading, string $text): void {
  echo '
<!DOCTYPE html><html><head>
  <meta charset="utf-8" />
  <title>Error page</title>
  <meta name="description" content="a generic error page" />  
  <link rel="icon" type="image/png" sizes="96x96" href="images/favicon.png" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link rel="stylesheet" href="css/font.css" type="text/css" />
  <link rel="stylesheet" href="css/normalize.css" type="text/css" />
  <link rel="stylesheet" href="css/skeleton.css" type="text/css" />';  
  echo "</head><body><div class=\"row twelve columns textBox\"><h4>$heading</h4><p>$text</p></div></body></html>";
  die();  
}



  session_start(); // this code must precede any HTML output  

  if (!getUserid()) {
        // there might be two reasons: 
        // - user is connecting directly to links.php from where-ever (common case as you might store the links-page as bookmark). If so, just redirect to index.php
        // - session is really destroyed (e.g. user logged out). In this case, print an error message
        printMsgAndDie(heading: 'Login error', text: 'did not get an user ID from the session');
  }
  
  printMsgAndDie(heading: 'All ok', text: 'Everything did work ok with starting the session');

  
