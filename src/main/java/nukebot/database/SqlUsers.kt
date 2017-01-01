package nukebot.database

import java.sql.SQLException

object SqlUsers {

    @Throws(SQLException::class)
    fun createUser(userID: String) {
        val statement = Database.conn
                ?.prepareStatement("INSERT INTO `users` (`uid`) VALUES (?)") ?: return

        statement.setString(1, userID)

        statement.executeUpdate()

        statement.close()
    }

    @Throws(SQLException::class)
    fun isUserPresent(userID: String): Boolean {
        val statement = Database.conn
                ?.prepareStatement("SELECT NULL FROM `users` WHERE `uid` = ?") ?: return false

        statement.setString(1, userID)

        val rs = statement.executeQuery()
        val isPresent = rs.first()

        rs.close()
        statement.close()

        return isPresent
    }

    @Throws(SQLException::class)
    fun setOperator(userID: String, value: Int) {
        val statement = Database.conn?.prepareStatement("UPDATE `users` SET `operator` = ? WHERE `uid` = ?") ?: return

        statement.setInt(1, value)
        statement.setString(2, userID)

        statement.executeUpdate()

        statement.close()
    }

    @Throws(SQLException::class)
    fun isOperator(userID: String): Boolean {
        val statement = Database.conn
                ?.prepareStatement("SELECT `operator` FROM `users` WHERE `uid` = ?") ?: return false

        statement.setString(1, userID)

        val rs = statement.executeQuery()
        val isOperator = rs.first() && rs.getInt(1) != 0

        rs.close()
        statement.close()

        return isOperator
    }

}
