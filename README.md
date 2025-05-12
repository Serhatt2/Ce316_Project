# 💻 Ce316_Project

## 📌 Must Read Before Use
##**PLEASE START THE APPLICATION IN ADMINISTRATOR MODE**

The application creates two folders in the `{user.home}\Documents` directory:

- 🗂️ **ConfigFiles**
- 🗂️ **ProjectFiles**

Whenever you create a project or a config.json file, the app will **automatically** direct the user to the appropriate folder.

---

### 🚀 To Create a New Project:

- `File → New Project → Fill the blanks`
- `Use Existing Config` → Choose a `config.json` file  
  *(automatically opens the **ConfigFiles** directory)*
- `Create Config` → Fill the blanks, then choose the save directory  
  *(automatically opens the **ConfigFiles** directory)*
- `Choose Project Folder to Save` →  
  *(automatically opens the **ProjectFiles** directory)*

- `Select Directory With ZIP` →  
  ❗ **⚠️ WARNING:** The application **copies** the selected ZIP files into the project folder and **deletes** the originals.  
  👉 We strongly recommend selecting a folder that contains **only the ZIP files**.

🟥 *For example: If you select your **Desktop** as the ZIP source, all files and shortcuts on your desktop will be copied into the project folder and then deleted!*

---

### 📂 To Open a Project:

- `File → Open Project → Choose the project folder`  
  *(Browse button will automatically open the **ProjectFiles** directory)*

❗ **⚠️ WARNING:** For best results, select the correct subfolder as shown below:

```text
ProjectFiles/
└── ProjectC/ ← ✅ Select this folder!
    ├── 20220602017.ZIP
    │   ├── main.c
    │   └── main.class
    ├── 20220602061.ZIP
    │   ├── main1.c
    │   └── main1.class
    └── configC.json
```

---

### ⚙️ Config File Actions:

- `Create Config` → Fill in the blanks and choose a save location  
  *(automatically opens the **ConfigFiles** directory)*

- `Edit Config` → The **Browse** button will automatically open **ConfigFiles**  
  👉 Select a `config_*.json` file to edit

- `Delete Config` → The **Browse** button will automatically open **ConfigFiles**  
  🗑️ Select a `config_*.json` file to delete

- `Export Config` → The application opens **ConfigFiles** by default  
  📤 Select the config file you want to export

❗ **⚠️ WARNING:**  
Creating config files **outside** the `ConfigFiles` directory will **break** the export feature.  
➡️ To ensure proper operation, always **save configs within the ConfigFiles folder**.
