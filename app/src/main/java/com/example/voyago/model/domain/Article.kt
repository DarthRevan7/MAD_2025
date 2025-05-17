<<<<<<< Updated upstream:app/src/main/java/com/example/voyago/model/Article.kt
package com.example.voyago.model

import android.media.Image
=======
package com.example.voyago.model.domain

>>>>>>> Stashed changes:app/src/main/java/com/example/voyago/model/domain/Article.kt
import java.util.Calendar

data class Article(
    val id: Int,
    var title: String,
    var text: String,
    var authorId: Int,
    var date: Calendar,
    var photos: List<Image>?,
    var views: Int
)