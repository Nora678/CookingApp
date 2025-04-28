Cooking App
Overview

Cooking App is a Java Swing-based application developed in 2025 by Nora Paskaleva as her Final Informatics Project.

Main Features
- Secure User Registration and Login (with BCrypt password hashing).
- Add, browse, edit, and delete cooking recipes.
- Manage your own ingredient inventory.
- Favorite and unfavorite recipes.
- Advanced search, sorting, and filtering options.
- Cookable recipes filter (based on available ingredients).
- Automatic session expiration after 5 minutes of inactivity.

Project Structure

Main.java - Application entry point.
Login.java - Handles user login.
Register.java - Manages new user registration with image upload.
Welcome.java - Main dashboard after successful login (Homepage).
AddRecipe.java - Form to create new recipes.
BrowseRecipes.java - Browse, search, sort, and filter recipes.
RecipeDetails.java - View and edit individual recipe details.
Favourites.java - Manage favorite recipes.
Inventory.java - Manage personal inventory of ingredients.
Navigator.java - Manages window navigation history.
connect.java - Database operations for login and registration.
Session.java - Stores session state for the logged-in user.
SessionTimer.java - Manages automatic session expiration.
UIGlobal.java - Centralizes UI styling (colors, fonts, icons).
User.java - Represents the user model with profile image.

Security:
Passwords are stored securely using BCrypt hashing.

All database queries use PreparedStatements to prevent SQL Injection.

Session Management:
Automatic logout after inactivity improves user security.
