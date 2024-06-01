package purpletreesoftware.karuahchess.database

class TableName(pActivityID: Int) {
    val Parameter: String
    val GameRecord: String

    init {
        if (pActivityID > 0) {
            Parameter = "Parameter_${pActivityID}"
            GameRecord = "GameRecord_${pActivityID}"
        }
        else {
            Parameter = "Parameter"
            GameRecord = "GameRecord"
        }
    }

}