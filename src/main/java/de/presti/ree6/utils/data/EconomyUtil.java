package de.presti.ree6.utils.data;

import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.economy.MoneyHolder;
import de.presti.ree6.sql.entities.economy.MoneyTransaction;
import net.dv8tion.jda.api.entities.Member;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

/**
 * Utility class for Economy related stuff.
 */
public class EconomyUtil {

    /**
     * Check if a MoneyHolder has any cash.
     *
     * @param member The Member.
     * @return If the MoneyHolder has any cash.
     */
    public static boolean hasCash(Member member) {
        return hasCash(member.getGuild().getIdLong(), member.getIdLong());
    }

    /**
     * Check if a MoneyHolder has any cash.
     *
     * @param guildId  The ID of the Guild.
     * @param memberId The ID of the Member.
     * @return If the MoneyHolder has any cash.
     */
    public static boolean hasCash(long guildId, long memberId) {
        return hasCash(getMoneyHolder(guildId, memberId));
    }

    /**
     * Check if a MoneyHolder has any cash.
     *
     * @param moneyHolder The MoneyHolder to check.
     * @return If the MoneyHolder has any cash.
     */
    public static boolean hasCash(MoneyHolder moneyHolder) {
        return moneyHolder != null && moneyHolder.getAmount() > 0;
    }

    /**
     * Retrieve a MoneyHolder from the Database.
     *
     * @param member The Member.
     * @return The MoneyHolder.
     */
    public static MoneyHolder getMoneyHolder(Member member) {
        return getMoneyHolder(member.getGuild().getIdLong(), member.getIdLong());
    }

    /**
     * Retrieve a MoneyHolder from the Database.
     *
     * @param guildId  The ID of the Guild.
     * @param memberId The ID of the Member.
     * @return The MoneyHolder.
     */
    public static MoneyHolder getMoneyHolder(long guildId, long memberId) {
        return getMoneyHolder(guildId, memberId, true);
    }

    /**
     * Retrieve a MoneyHolder from the Database.
     *
     * @param guildId           The ID of the Guild.
     * @param memberId          The ID of the Member.
     * @param createIfNotExists If the MoneyHolder should be created if it does not exist.
     * @return The MoneyHolder.
     */
    public static MoneyHolder getMoneyHolder(long guildId, long memberId, boolean createIfNotExists) {
        MoneyHolder moneyHolder = SQLSession.getSqlConnector().getSqlWorker().getEntity(new MoneyHolder(), "FROM MoneyHolder WHERE guildUserId.guildId = :gid AND guildUserId.userId = :uid",
                Map.of("gid", guildId, "uid", memberId));

        if (moneyHolder == null && createIfNotExists) {
            moneyHolder = new MoneyHolder();
            moneyHolder.setGuildId(guildId);
            moneyHolder.setUserId(memberId);
            moneyHolder = SQLSession.getSqlConnector().getSqlWorker().updateEntity(moneyHolder);
        }

        return moneyHolder;
    }

    /**
     * Check if the MoneyHolder has enough money.
     *
     * @param target    The MoneyHolder to check.
     * @param amount    The amount to check.
     * @param checkBank If the bank should be checked.
     * @return If the MoneyHolder has enough money.
     */
    public static boolean hasEnoughMoney(MoneyHolder target, double amount, boolean checkBank) {
        return checkBank ? target.getBankAmount() >= amount : target.getAmount() >= amount;
    }

    /**
     * Create a payment transaction between two MoneyHolders.
     *
     * @param sender   The sender of the money.
     * @param receiver The receiver of the money.
     * @param amount   The amount of money to send.
     * @param fromBank If the money should be taken from the bank.
     * @param toBank   If the money should be sent to the bank.
     * @return If the payment was successful.
     */
    public static boolean pay(MoneyHolder sender, MoneyHolder receiver, double amount, boolean fromBank, boolean toBank) {
        return pay(sender, receiver, amount, fromBank, toBank, false);
    }

    /**
     * Set the amount of money for a MoneyHolder.
     *
     * @param holder  The sender of the money.
     * @param amount  The amount of money to send.
     * @param setBank If the money should be put into the bank.
     * @return If the set was successful.
     */
    public static boolean set(MoneyHolder holder, double amount, boolean setBank) {
        if (holder == null || amount < 0) {
            return false;
        }

        if (setBank) {
            holder.setBankAmount(amount);
        } else {
            holder.setAmount(amount);
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(holder);
        return true;
    }

    /**
     * Create a payment transaction between two MoneyHolders.
     *
     * @param sender   The sender of the money.
     * @param receiver The receiver of the money.
     * @param amount   The amount of money to send.
     * @param fromBank If the money should be taken from the bank.
     * @param toBank   If the money should be sent to the bank.
     * @param isSystem If the payment is a system payment.
     * @return If the payment was successful.
     */
    public static boolean pay(MoneyHolder sender, MoneyHolder receiver, double amount, boolean fromBank, boolean toBank, boolean isSystem) {
        if (!isSystem && !hasEnoughMoney(sender, amount, fromBank)) {
            return false;
        }

        if (toBank) {
            receiver.setBankAmount(receiver.getBankAmount() + amount);
        } else {
            receiver.setAmount(receiver.getAmount() + amount);
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(receiver);

        if (!isSystem) {
            if (fromBank) {
                sender.setBankAmount(sender.getBankAmount() - amount);
            } else {
                sender.setAmount(sender.getAmount() - amount);
            }

            SQLSession.getSqlConnector().getSqlWorker().updateEntity(sender);
        }

        SQLSession.getSqlConnector().getSqlWorker().updateEntity(new MoneyTransaction(0L, isSystem, isSystem ? receiver.getGuildUserId().getGuildId() : sender.getGuildUserId().getGuildId(), isSystem && sender == null ? receiver : sender, receiver, toBank, fromBank, amount, Timestamp.from(Instant.now())));

        return true;
    }

    public static String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }

}
