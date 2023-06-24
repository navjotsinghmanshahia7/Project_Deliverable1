// GoFishCard.java
package ca.sheridancollege.project;

/**
 * A class representing a Go Fish card. It extends the base Card class.
 */
public class GoFishCard extends Card {

    private final Rank rank;
    private final Suit suit;

    public GoFishCard(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}

// GoFishGame.java
package ca.sheridancollege.project;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * A class representing the Go Fish game. It extends the base Game class.
 */
public class GoFishGame extends Game {

    private final int NUM_RANKS = Rank.values().length;
    private final int NUM_SUITS = Suit.values().length;

    private final GroupOfCards deck;
    private final ArrayList<GoFishPlayer> goFishPlayers;
    private GoFishPlayer currentPlayer;

    public GoFishGame(String name, int numPlayers) {
        super(name);
        goFishPlayers = new ArrayList<>();
        deck = new GroupOfCards(NUM_RANKS * NUM_SUITS);
        initializeDeck();
        initializePlayers(numPlayers);
        currentPlayer = goFishPlayers.get(0);
    }

    private void initializeDeck() {
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                GoFishCard card = new GoFishCard(rank, suit);
                deck.getCards().add(card);
            }
        }
        deck.shuffle();
    }

    private void initializePlayers(int numPlayers) {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter player " + (i + 1) + " name: ");
            String name = scanner.nextLine();
            GoFishPlayer player = new GoFishPlayer(name);
            goFishPlayers.add(player);
        }
    }

    @Override
    public void play() {
        int numTurns = 0;
        while (!deck.getCards().isEmpty() && !allPlayersOutOfCards()) {
            numTurns++;
            System.out.println("\nTurn " + numTurns + ":");

            currentPlayer.play(deck, goFishPlayers);

            currentPlayer = getNextPlayer();
        }
    }

    @Override
    public void declareWinner() {
        GoFishPlayer winner = null;
        int maxSets = 0;
        for (GoFishPlayer player : goFishPlayers) {
            int numSets = player.getNumberOfSets();
            System.out.println(player.getName() + " has " + numSets + " sets.");
            if (numSets > maxSets) {
                maxSets = numSets;
                winner = player;
            }
        }
        if (winner != null) {
            System.out.println("\n" + winner.getName() + " wins the game!");
        } else {
            System.out.println("\nIt's a tie!");
        }
    }

    private GoFishPlayer getNextPlayer() {
        int currentIndex = goFishPlayers.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % goFishPlayers.size();
        return goFishPlayers.get(nextIndex);
    }

    private boolean allPlayersOutOfCards() {
        for (GoFishPlayer player : goFishPlayers) {
            if (!player.getHand().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * A nested class representing a player in the Go Fish game. It extends the base Player class.
     */
    private class GoFishPlayer extends Player {

        private final ArrayList<GoFishCard> hand;
        private final ArrayList<Rank> sets;

        public GoFishPlayer(String name) {
            super(name);
            hand = new ArrayList<>();
            sets = new ArrayList<>();
        }

        public ArrayList<GoFishCard> getHand() {
            return hand;
        }

        public int getNumberOfSets() {
            return sets.size();
        }

        @Override
        public void play() {
            // Not used in Go Fish
        }

        public void play(GroupOfCards deck, ArrayList<GoFishPlayer> players) {
            System.out.println(getName() + "'s turn:");
            System.out.println("Hand: " + hand);
            System.out.println("Sets: " + sets);

            if (hand.isEmpty()) {
                System.out.println("No cards in hand. Drawing a card...");
                drawCard(deck);
            }

            Rank rankToAsk = chooseRankToAsk();
            GoFishPlayer playerToAsk = choosePlayerToAsk(players);

            System.out.println(getName() + " asks " + playerToAsk.getName() + " for " + rankToAsk);
            int numCardsReceived = playerToAsk.giveCards(rankToAsk, hand);
            System.out.println(playerToAsk.getName() + " gives " + getName() + " " + numCardsReceived + " card(s).");

            if (numCardsReceived == 0) {
                System.out.println(playerToAsk.getName() + " says, \"Go Fish!\"");
                drawCard(deck);
            } else {
                System.out.println("Checking for sets...");
                checkForSets();
            }

            System.out.println(getName() + "'s turn is over.");
        }

        private Rank chooseRankToAsk() {
            return hand.get(0).getRank(); // Simple strategy: always ask for the rank of the first card in hand
        }

        private GoFishPlayer choosePlayerToAsk(ArrayList<GoFishPlayer> players) {
            int currentPlayerIndex = players.indexOf(currentPlayer);
            int nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
            return players.get(nextPlayerIndex);
        }

        private void drawCard(GroupOfCards deck) {
            GoFishCard card = (GoFishCard) deck.getCards().remove(0);
            hand.add(card);
            System.out.println(getName() + " draws a card: " + card);
        }

        private int giveCards(Rank rank, ArrayList<GoFishCard> opponentHand) {
            int numCardsGiven = 0;
            ArrayList<GoFishCard> cardsToRemove = new ArrayList<>();

            for (GoFishCard card : opponentHand) {
                if (card.getRank() == rank) {
                    cardsToRemove.add(card);
                    numCardsGiven++;
                }
            }

            opponentHand.removeAll(cardsToRemove);
            hand.addAll(cardsToRemove);

            return numCardsGiven;
        }

        private void checkForSets() {
            ArrayList<Rank> ranks = new ArrayList<>();
            for (GoFishCard card : hand) {
                ranks.add(card.getRank());
            }

            for (Rank rank : Rank.values()) {
                int count = Collections.frequency(ranks, rank);
                if (count == 4) {
                    removeCardsOfRank(rank);
                    sets.add(rank);
                    System.out.println(getName() + " completes a set of " + rank + "s!");
                }
            }
        }

        private void removeCardsOfRank(Rank rank) {
            ArrayList<GoFishCard> cardsToRemove = new ArrayList<>();

            for (GoFishCard card : hand) {
                if (card.getRank() == rank) {
                    cardsToRemove.add(card);
                }
            }

            hand.removeAll(cardsToRemove);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
