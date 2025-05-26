package com.example.voyago.model

class UserModel {
    private var _users = privateUsers
    var users = _users

    //SUBSET OF USER LIST

    //Get a list of user given their ids
    fun getUsers(ids: List<Int>): List<UserData> {
        return _users.value.filter { it.id in ids }
    }

    //GET USER INFORMATION

    //Get a user given its id
    fun getUserDataById(id: Int): UserData {
        return _users.value.find { it.id == id }
            ?: throw NoSuchElementException("User with ID $id not found")
    }

    //EDIT USER

    //Edit user information
    fun editUserData(updatedUserData: UserData): List<UserData> {
        _users.value = _users.value.map {
            if (it.id == updatedUserData.id) updatedUserData else it
        }
        return _users.value
    }

    // Calculate average user rating by id
    fun refreshAllRatings(reviewModel: ReviewModel) {
        _users.value = _users.value.map { user ->
            user.copy(rating = reviewModel.calculateRatingById(user.id))
        }
    }
}