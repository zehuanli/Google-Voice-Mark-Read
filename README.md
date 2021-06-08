# Description
Up till now Google Voice does not provide the basic feature to mark messages as read in the notifications. Tired of going into the app and clicking on each unread message every time, I made this Xposed module to add a "mark as read" option to the Google Voice message notifications.

This module has been tested working for the current Google Voice version. It may stop working for the future versions since the package structure may change. However, it shouldn't be hard to tweak the module to support the new versions as long as there's no major change on the notification logics in Google Voice.

# A little bit details
The "mark as read" option utilizes a hidden notification action provided by Google Voice, which is highly likely to be used by Android Auto, since it's inside a bundle named `android.car.EXTENSIONS`. It is *possible* to implement the module without relying on this hidden action, but there definitely will be more reverse engineering work to do.

Some reverse engineering tips (commented in the codes as well):
  - Search for "notify" for use of `Notification` class
  - Search for "addAction" for adding actions (buttons on the pop-up)
  - Packages start with `defpackage` should be named without this prefix
