package com.app.tintuccongnghe.data.local.dao

import androidx.room.*
import com.app.tintuccongnghe.data.local.entities.DetailRuleEntity

@Dao
interface DetailRuleDao {
    @Query("SELECT * FROM detail_rules_table WHERE websiteId = :websiteId")
    suspend fun getRulesForWebsite(websiteId: String): List<DetailRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<DetailRuleEntity>)

    @Query("DELETE FROM detail_rules_table WHERE websiteId = :websiteId")
    suspend fun deleteForWebsite(websiteId: String)
}
