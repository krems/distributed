package com.ovchinnikov.paxos;

import com.ovchinnikov.reactor.Reactor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Proposer<T> {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private final Reactor reactor = new Reactor(() -> {
    }, Integer.MAX_VALUE);
    private final Set<Acceptor<T>> nodes;
    private final String name = String.valueOf(ID_GENERATOR.incrementAndGet());

    private int accepted = 0;
    private int rejected = 0;
    private final Set<Proposal<T>> commited = new HashSet<>();
    private final Set<Proposal<T>> notCommited = new HashSet<>();

    private ProposalId id = ProposalId.of(name, 1);
    private Proposal<T> currentProposal;
    private Proposal<T> acceptedNotCommitedProposal;
    private Proposal<T> commitedProposal;

    public Proposer(Set<Acceptor<T>> nodes) {
        this.nodes = nodes;
    }

    public void start() {
        reactor.start();
    }

    public void stop() {
        reactor.stop();
    }

    public void propose(T value) {
        reactor.submit(() -> {
            propose(value);
        });
    }

    private void doPropose(T value) {
        updateId(id);
        Proposal<T> proposal = new Proposal<>(id, value);
        for (Acceptor<T> node : nodes) {
            node.receive(proposal, this);
        }
    }

    public void onAccept(T value) {
        reactor.submit(() -> {
            accepted++;
            if (!value.equals(currentProposal.value)) {
                acceptedNotCommitedProposal = new Proposal<>(currentProposal.id, value);
            }
            if (isMajorityAccepted()) {
                onMajorityAgree();
            }
        });
    }

    private boolean isMajorityAccepted() {
        return accepted > nodes.size() / 2; // todo: check in these methods that value is the same
    }

    public void onReject(ProposalId id) {
        reactor.submit(() -> {
            updateId(id); // todo?
            rejected++;
            if (isMajorityRejected()) {
                onMajorityReject();
            }
        });
    }

    private boolean isMajorityRejected() {
        return rejected > nodes.size() / 2;
    }

    private void onMajorityAgree() {
        accepted = 0;
        rejected = 0;
        System.err.println("Accepted: " + currentProposal);
        if (acceptedNotCommitedProposal != null) {
            doCommit(acceptedNotCommitedProposal);
            acceptedNotCommitedProposal = null;
        } else {
            doCommit(currentProposal);
        }
    }

    private void onMajorityReject() {
        accepted = 0;
        rejected = 0;
        System.err.println("Failed to propose: " + currentProposal);
        currentProposal = null;
    }

    private void doCommit(Proposal<T> proposal) {
        for (Acceptor<T> node : nodes) {
            node.commit(proposal, this);
        }
    }

    public void onCommit(Proposal<T> proposal) {
        reactor.submit(() -> {
            commited.add(proposal);
            if (!proposal.equals(currentProposal)) {
                commitedProposal = proposal;
            }
            if (isMajorityCommited()) {
                onMajorityCommited();
            }
        });
    }

    private boolean isMajorityCommited() {
        return commited.size() > nodes.size() / 2;
    }

    public void onNotCommit(Proposal<T> proposal) {
        reactor.submit(() -> {
            accepted = 0;
            rejected = 0;
            notCommited.add(proposal);
            if (isMajorityNotCommited()) {
                onMajorityNotCommited();
            }
        });
    }

    private boolean isMajorityNotCommited() {
        return notCommited.size() > nodes.size() / 2;
    }

    private void onMajorityCommited() {
        accepted = 0;
        rejected = 0;
        System.out.println("Commited: " + commitedProposal);
        if (commitedProposal == currentProposal) {
            currentProposal = null;
        } else {
            doCommit(currentProposal);
        }
        commitedProposal = null;
    }

    private void onMajorityNotCommited() {
        System.err.println("Not commited: " + currentProposal);
        currentProposal = null;
    }

    private ProposalId updateId(ProposalId oldId) {
        id = ProposalId.getGreater(name, oldId);
        return id;
    }
}
