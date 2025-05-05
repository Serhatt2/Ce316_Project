# Ce316_Project
## Must Read Before Use
  The application creates two files in the "{user.home}\documents" directory as "ConfigFiles" and "ProjectFiles"
  Whenever you create a project or a config.json it automaticaly directs user to corresponding file.

  To create new project:
- File -> New Project -> Fill the blanks
- Use Existing Config -> Choose an config.json file(automaticaly directs you to the ConfigFiles directory=
- Create Config -> Fill the blanks than choose the directory to save(automaticaly directs you to the ConfigFiles directory)
- Choose the porject folder to save -> automaticaly directs you to the ProjectFile directory.
- Select Directory With ZIP -> **WARNING** Since application copies the zip files into the project folder then deletes the originals we reccomend users to select a file which contains only the ZIP files.
  
*For ex: If you would choose the "Desktop" directory as "ZIP files directory" it will copy all the files and shortcuts into the project file then deletes the content of the desktop*  

__________________________________________________________________________________________________________________________________________________
  To open project:
- File -> Open Project -> Choose the porject folder (Browse button will automaticaly directs you to the ProjectFiles directory.
**WARNING** For better usage please select the corresponding folder as shown below
ProjectC/  ----------------> Select this file
├── 20220602017.ZIP
│   ├── main.c
│   └── main.class
├── 20220602061.ZIP
│   ├── main1.c
│   └── main1.class
└── configC.json
__________________________________________________________________________________________________________________________________________________
  Config File Actions:
- Create Config -> Fill the blanks than choose the directory to save(automaticaly directs you to the ConfigFiles directory)
- Edit Config -> Browse Button will automaticaly directs you to the ConfigFiles directory. Choose a config_.json file to edit.
- Delete Config -> Browse Button will automaticaly directs you to the ConfigFiles directory. Choose a config_.json file to delete.
- Export Config -> Application will automaticaly open the ConfigFiles directory. **Since export button opens the ConfigFiles directory, creating new configs out of the ConfigFiles directory will bypass the efficiency of the Export Button**
__________________________________________________________________________________________________________________________________________________
