package com.my.notificationai.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rule_conditions",
    foreignKeys = [
        ForeignKey(
            entity = BlockingRule::class,
            parentColumns = ["id"],
            childColumns = ["rule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["rule_id"], name = "index_conditions_rule_id")
    ]
)
data class RuleCondition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "rule_id")
    val ruleId: Long,
    @ColumnInfo(name = "condition_type")
    val conditionType: String, // APP, TITLE, TEXT, KEYWORD, CHANNEL, TIME_RANGE, DAY_OF_WEEK, ONGOING
    @ColumnInfo(name = "operator")
    val operator: String, // EQUALS, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH, IN_RANGE
    @ColumnInfo(name = "value")
    val value: String
)
