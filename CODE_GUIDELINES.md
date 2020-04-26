# Code writing guidelines
Basically, use the Kotlin naming convention and keep the curly bracket on the same line as the function declaration.


### This project's special functions

There's a bunch of convenient functions in [Tools.kt](app/src/main/java/posidon/launcher/tools/Tools.kt), use those.

```kotlin
// For example, instead of writing
8 * resources.displayMetrics.density

// you can write
8.dp

// Or you can also convert drawables to bitmaps using
someDrawable.toBitmap()
```

### Avoid adding unnecessary dependencies

The less dependencies, the better.
Only add dependencies if they're essential.

Google services dependencies are forbidden