package me.ywsmithing;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Menu;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.ui.Message;

import java.awt.*;
import java.text.DecimalFormat;

// https://explv.github.io/?centreX=3172&centreY=3331&centreZ=0&zoom=7 Kordynaty Mapy
// ALT + ENTER importuje biblioteki
@ScriptManifest(name = "YW_Smithing", info = "Smithing from Ore to Bars", author = "YmirWork", version = 0.1, logo = "")
public class Main extends Script {

    private Area smeltingArea = new Area(3105, 3501, 3110, 3496),
            bankArea = Banks.EDGEVILLE,
            exchangeArea = Banks.GRAND_EXCHANGE;   // Area(3160, 3484, 3169, 3494);


    long coins = 100;
    int silverBarsSell = 75;
    int silverOre = 28;
    int smeltOre = 0;
    private String[] typeOfOre = {"Iron Ore", "Silver Ore", "Gold Ore"};

    boolean inputAccepted = true;

    int startingLevelSM;
    long startTime, testTime;

    String makingAction = "Null";


    @Override
    public int onLoop() throws InterruptedException {
        NPC bank = getNpcs().closest("Banker");
        NPC clerk = getNpcs().closest("Grand Exchange Clerk");
        walkToBankArea();
        openBank();
        withdrawItems();
        smelting();
        walkToBankArea();
        openBank();
        bankDeposit();
        return random(1200, 1800);
    }


    public void bankDeposit() {
        // Jezeli mamy Silver Bar 14 i nie mamy silver Ore w ekwipubku i jeżeli jesteśmy w lokalizacji Banku i bank jest otwarty wtedy wykonujemy kod
        if (inventory.getAmount("Silver Bar") == 28 && !inventory.contains("Silver Ore") && bankArea.contains(myPosition()) && bank != null && getBank().isOpen()) {
            log("Deposit Silver Bar");
            makingAction = "Deposit Silver Bar";
            getBank().depositAll("Silver Bar");
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return !inventory.contains("Silver Bar");
                }
            }.sleep();
        }

    }

    public void walkToBankArea() {
        NPC bank = getNpcs().closest("Banker");
        NPC clerk = getNpcs().closest("Grand Exchange Clerk");
        // Sprawdzamy czy Ekwipunek jest Pusty i czy posiada 14 Silver bar i czy jest w lokalizacji Banku NIE JEST więc idziemy do banku.
        if (inventory.isEmpty() || inventory.getAmount("Silver Bar") == 28 && !bankArea.contains(myPosition())) {
            log("Walking to Bank Area");
            makingAction = "Walking to Bank Area";
            getWalking().webWalk(bankArea);
            new ConditionalSleep(2000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return bankArea.contains(myPosition());

                }
            }.sleep();
            // Jezeli jestesmy w lokalizacji bankArea i nie widzimy banku wtedy dostosowywujemy kamere do banku.
        } else if (bankArea.contains(myPosition()) && bank != null && !bank.isVisible()) {
            getCamera().toEntity(bank);

        }
    }

    public void withdrawItems() {
        // Jeżeli ekwupinek jest pusty ale jesteśmy w lokalizacji banku bank jest null i jeżeli bank jest otwarty.
        if (inventory.isEmpty() && bankArea.contains(myPlayer()) && bank != null && getBank().isOpen()) {
            log("Withdraw Silver Ore");
            makingAction = "Withdraw Silver Ore";
            getBank().withdrawAll("Silver Ore");
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return inventory.contains("Silver Ore");
                }
            }.sleep();
        }

    }

    public void openBank() {
        NPC bank = getNpcs().closest("Banker");
        // || inventory.getAmount("Silver Ore") == 28 && inventory.getAmount("Silver Ore" != 28)))
        if ((inventory.isEmpty() || (inventory.getAmount("Silver bar") == 28) && bankArea.contains(myPlayer()) && bank != null && bank.isVisible() && !getBank().isOpen())) {
            log("Open Bank");
            makingAction = "Open Bank";
            bank.interact("Bank");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return getBank().isOpen();
                }
            }.sleep();
        }
    }

    public void smelting() {
        RS2Object furnace = getObjects().closest("Furnace");
        RS2Widget silverWidget = getWidgets().get(270, 16, 29);
        // Sprawdzamy czy mamy Silver Ore i jeżęli nie jesteśmy w smelting Are wykonujemy kod aby szedł do smeltingArea
        if (inventory.contains("Silver Ore") && !smeltingArea.contains(myPlayer())) {
            log("Walking to Smealting Area");
            makingAction = "Walking to Smealting Area";
            getWalking().webWalk(smeltingArea);
            new ConditionalSleep(30000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return smeltingArea.contains(myPlayer());
                }
            }.sleep();

        } // jeżeli jesteśmy w smeltingArea i furnace jest null i nie widać furnace wtedy dostosowywujemy kamere do furnace
        else if (smeltingArea.contains(myPlayer()) && furnace != null && !furnace.isVisible()) {
            getCamera().toEntity(furnace);
        }
        //Sprwadzamy czy posiadamy silver ore oraz czy jesteśmy w smeltingArea czy nie wykonujemy animacje i czy furnace jest widzialny
        if (inventory.contains("Silver Ore") && smeltingArea.contains(myPlayer()) && furnace != null && silverWidget == null && !myPlayer().isAnimating() && furnace.isVisible()) {
            log("Start Smelting ");
            makingAction = "Start Smelting";
            furnace.interact("Smelt");
            new ConditionalSleep(5000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return getDialogues().isPendingContinuation() || silverWidget != null;
                }
            }.sleep();
        } else if (inventory.contains("Silver Ore") && smeltingArea.contains(myPlayer()) && !myPlayer().isAnimating() && silverWidget != null) {
            silverWidget.interact();
            new ConditionalSleep(60000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return !inventory.contains("Silver Ore") || getDialogues().isPendingContinuation();
                }
            }.sleep();
        }


    }

    public final String formatTime(final long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60;
        m %= 60;
        h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    public void onMessage(Message m) {
        if (inputAccepted) {
            if (m.getMessage().contains("You retrieve a bar")) {
                smeltOre += 1;
            }
        }
    }

    @Override
    public void onExit() throws InterruptedException {
        log("Stopping Script.");
    }

    @Override
    public void onStart() throws InterruptedException {
        log("Starting Script");
        getExperienceTracker().start(Skill.SMITHING);
        startingLevelSM = getSkills().getStatic(Skill.SMITHING);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPaint(Graphics2D g) {
        String curretTime = formatTime(System.currentTimeMillis() - startTime);
        testTime = (System.currentTimeMillis() - startTime) / 60000;
        g.setColor(Color.BLACK);
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(7, 345, 506, 130);

        Font font = new Font("Times New Roman", Font.BOLD, 22);
        g.setFont(font);

        g.setColor(Color.WHITE);
        g.drawString("YW Smithing AIO ", 20, 365);

        font = new Font("Arial", Font.PLAIN, 14);
        g.setFont(font);
        g.drawString("Time run: " + curretTime, 20, 385);


        g.drawString("Ore smelted: " + smeltOre, 20, 400);
        g.drawString("Smithing XP Gained: " + getExperienceTracker().getGainedXP(Skill.SMITHING), 20, 415);
        g.drawString("XP Per Hour: " + getExperienceTracker().getGainedXPPerHour(Skill.SMITHING), 20, 430);
        g.drawString("Started At: " + startingLevelSM + " Level", 20, 445);
        g.drawString("Action: " + makingAction, 20, 460);

    }

}

