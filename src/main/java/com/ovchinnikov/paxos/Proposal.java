package com.ovchinnikov.paxos;

import java.util.Objects;

public class Proposal<T> implements Comparable<Proposal> {
    final T value;
    final ProposalId id;

    public Proposal(ProposalId id, T value) {
        Objects.requireNonNull(value);
        this.value = value;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Proposal<?> proposal = (Proposal<?>) o;

        if (!value.equals(proposal.value)) return false;
        return id.equals(proposal.id);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "value=" + value +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int compareTo(Proposal o) {
        return id.compareTo(o.id);
    }
}
