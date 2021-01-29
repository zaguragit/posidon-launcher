# Contribution guidelines

#### Code style rules:
- Please write the curly bracket on the same line as the function declaration.
- Use the normal, Java/Kotlin naming convention

### Dependencies

Avoid adding dependencies for trivial things, like image loading
All dependencies must be open-source and free

### Convenient functions

There's a bunch of utility functions in the [tools folder](app/src/main/java/posidon/launcher/tools), use those if you can.

```kotlin
// For example, instead of writing
8 * resources.displayMetrics.density

// you can write
8.dp

// Or you can also convert drawables to bitmaps using
someDrawable.toBitmap()
```
