# Contribution guidelines

I do accept Java contributions, since Kotlin's fully interoperable with Java, but I'll still translate them.

#### Code style rules:
- Please write the curly bracket on the same line as the function declaration.
- Use the normal, Java/Kotlin naming convention

### Avoid adding unnecessary dependencies

The less dependencies, the better.
Only add dependencies if they're essential.
All dependencies have to be open-source and free

### Convenient functions

There's a bunch of very convenient functions in the [tools folder](app/src/main/java/posidon/launcher/tools), use those if you can.

```kotlin
// For example, instead of writing
8 * resources.displayMetrics.density

// you can write
8.dp

// Or you can also convert drawables to bitmaps using
someDrawable.toBitmap()
```