package com.ovchinnikov.paxos;

public class ProposalId implements Comparable<ProposalId> {
    private static final ProposalId MIN = new ProposalId("", 0);
    private final String name;
    private final long seqNum;

    private ProposalId(String name, long seqNum) {
        this.name = name;
        this.seqNum = seqNum;
    }

    public static ProposalId of(String name, long seqNum) {
        return new ProposalId(name, seqNum);
    }

    public static ProposalId getGreater(String name, ProposalId proposalId) {
        return ProposalId.of(name, proposalId.seqNum + 1);
    }

    public static ProposalId min() {
        return MIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProposalId that = (ProposalId) o;

        if (seqNum != that.seqNum) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (seqNum ^ (seqNum >>> 32));
        return result;
    }

    @Override
    public int compareTo(ProposalId o) {
        if (o == MIN) {
            return 1;
        }
        if (this == MIN) {
            return -1;
        }
        if (seqNum == o.seqNum) {
            return name.compareTo(o.name);
        }
        return seqNum > o.seqNum ? 1 : -1;
    }

    @Override
    public String toString() {
        return name + seqNum;
    }
}
