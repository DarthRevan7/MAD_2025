package com.example.voyago

fun formatFiltersPage(input: String): Pair<Int,Int>? {
    if (input[0].isDigit()){
        val dashIndex = input.indexOf('-')

        if (dashIndex <= 0) {
            return null
        }

        val num1Str = input.substring(0,dashIndex)
        val startIndexNum2 = dashIndex+1

        if (startIndexNum2 >= input.length) {
            return null
        }

        var endIndexNum2 = startIndexNum2

        while(endIndexNum2 < input.length && input[endIndexNum2].isDigit()) {
            endIndexNum2++
        }

        val num2Str = input.substring(startIndexNum2, endIndexNum2)

        if (num2Str.isEmpty()) {
            return null
        }

        try {
            val num1 = num1Str.toInt()
            val num2 = num2Str.toInt()

            return Pair(num1, num2)
        }

        catch (exception: NumberFormatException) {
            println("Invalid extracted parts.  Input: \"$input\"")
            exception.printStackTrace()
            return null
        }
    }
    else if(input[0] == '>') {

        val startIndexNum = 2

        var endIndexNum = startIndexNum

        while(endIndexNum < input.length && input[endIndexNum].isDigit()) {
            endIndexNum++
        }

        val numStr = input.substring(startIndexNum, endIndexNum)

        try {
            val num = numStr.toInt()

            return Pair(num, -1)
        }

        catch (exception: NumberFormatException) {
            println("Invalid extracted parts.  Input: \"$input\"")
            exception.printStackTrace()

            return null
        }
    }
    else {
        println("Unexpected error.  Input: \"$input\"")
        return null
    }
}