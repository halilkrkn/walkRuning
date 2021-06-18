package com.example.walkruning.repositories

import com.example.walkruning.db.Running
import com.example.walkruning.db.RunningDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
        private val runDao: RunningDAO
) {
    suspend fun insertRunning(run:Running) = runDao.insertRunning(run)
    suspend fun deleteRunning(run:Running) = runDao.deleteRunning(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByDistanceInMeters() = runDao.getAllRunsSortedByDistanceInMeters()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistanceInMeters() = runDao.getTotalDistanceInMeters()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

}