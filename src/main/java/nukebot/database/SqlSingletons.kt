package nukebot.database

import java.sql.SQLException

object SqlSingletons {

    @Throws(SQLException::class)
    fun ensureExists() {
        val statement = Database.conn?.prepareStatement("SELECT `_` FROM `singleton`") ?: return

        val rs = statement.executeQuery()
        val hasFirst = rs.first()
        rs.close()
        if (!hasFirst)
            statement.executeUpdate(
                    "INSERT INTO `singleton` (`_`, `cahchannel`, `cahdecks`, `cahconfig`) VALUES (0, '', '', '')")

        statement.close()
    }

}
