package com.ovchinnikov.paxos;

import com.ovchinnikov.reactor.Reactor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class Acceptor<T> {
    private final Reactor reactor = new Reactor(() -> {}, Integer.MAX_VALUE);
    private final String id;
    private final Deque<T> accepted = new ArrayDeque<>();
    private ProposalId highestAcceptedId = ProposalId.min();

    public Acceptor(String id) {
        this.id = id;
    }

    public void start() {
        reactor.start();
    }

    public void stop() {
        reactor.stop();
    }

    public void receive(Proposal<T> proposal, Proposer<T> proposer) {
        reactor.submit(() -> {
            doReceiveProposal(proposal, proposer);
        });
    }

    private void doReceiveProposal(Proposal<T> proposal, Proposer<T> proposer) {
        if (accepted.isEmpty()) {
            accept(proposal);
            proposer.onAccept(proposal.value);
            return;
        }
        if (highestAcceptedId.compareTo(proposal.id) < 0) {
            accept(proposal);
            proposer.onAccept(accepted.getFirst());
            return;
        }
        proposer.onReject(highestAcceptedId);
        System.out.println("Rejected " + proposal + " by " + id);
    }

    private void accept(Proposal<T> proposal) {
        accepted.addLast(proposal.value);
        highestAcceptedId = proposal.id;
        System.out.println("Accepted " + proposal + " by " + id);
    }

    public void commit(Proposal<T> proposal, Proposer<T> proposer) {
        reactor.submit(() -> {
            doCommit(proposal, proposer);
        });
    }

    private void doCommit(Proposal<T> proposal, Proposer<T> proposer) {
        if (highestAcceptedId.compareTo(proposal.id) > 0) {
            notCommit(proposal, proposer);
            return;
        }
        if (accepted.isEmpty()) {
            notCommit(proposal, proposer);
            return;
        }
        Iterator<T> it = accepted.iterator();
        while (it.hasNext()) {
            T value = it.next();
            if (value.equals(proposal.value)) {
                it.remove();
                System.out.println("Commited " + proposal + " by " + id);
                proposer.onCommit(proposal);
                return;
            }
        }
        notCommit(proposal, proposer);
    }

    private void notCommit(Proposal<T> proposal, Proposer<T> proposer) {
        System.out.println("Not commited " + proposal + " by " + id);
        proposer.onNotCommit(proposal);
    }
}
