package edu.stanford.bmir.protegex.chao.annotation.api;

import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAdvice;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAgreeDisagreeVote;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAgreeDisagreeVoteProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultChangeHierarchyProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultComment;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultExample;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultExplanation;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultFiveStarsVote;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultFiveStarsVoteProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultLinguisticEntity;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultMergeProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultNewEntityProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultPropertyValueChangeProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultQuestion;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultRetireProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultReview;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultReviewRequest;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultSeeAlso;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultSimpleProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultSplitProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultStatus;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultVote;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultVotingProposal;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;

/**
 * Generated by Protege (http://protege.stanford.edu).
 *
 * @version generated on Mon Aug 18 21:11:09 GMT-08:00 2008
 */
public class OntologyJavaMapping {

    public static void initMap() {
        OntologyJavaMappingUtil.add("Advice", Advice.class, DefaultAdvice.class);
        OntologyJavaMappingUtil.add("AgreeDisagreeVote", AgreeDisagreeVote.class, DefaultAgreeDisagreeVote.class);
        OntologyJavaMappingUtil.add("AgreeDisagreeVoteProposal", AgreeDisagreeVoteProposal.class, DefaultAgreeDisagreeVoteProposal.class);
        OntologyJavaMappingUtil.add("AnnotatableThing", AnnotatableThing.class, DefaultAnnotatableThing.class);
        OntologyJavaMappingUtil.add("Annotation", Annotation.class, DefaultAnnotation.class);
        OntologyJavaMappingUtil.add("ChangeHierarchyProposal", ChangeHierarchyProposal.class, DefaultChangeHierarchyProposal.class);
        OntologyJavaMappingUtil.add("Comment", Comment.class, DefaultComment.class);
        OntologyJavaMappingUtil.add("Example", Example.class, DefaultExample.class);
        OntologyJavaMappingUtil.add("Explanation", Explanation.class, DefaultExplanation.class);
        OntologyJavaMappingUtil.add("FiveStarsVote", FiveStarsVote.class, DefaultFiveStarsVote.class);
        OntologyJavaMappingUtil.add("FiveStarsVoteProposal", FiveStarsVoteProposal.class, DefaultFiveStarsVoteProposal.class);
        OntologyJavaMappingUtil.add("LinguisticEntity", LinguisticEntity.class, DefaultLinguisticEntity.class);
        OntologyJavaMappingUtil.add("MergeProposal", MergeProposal.class, DefaultMergeProposal.class);
        OntologyJavaMappingUtil.add("NewEntityProposal", NewEntityProposal.class, DefaultNewEntityProposal.class);
        OntologyJavaMappingUtil.add("Proposal", Proposal.class, DefaultProposal.class);        
        OntologyJavaMappingUtil.add("PropertyValueChangeProposal", PropertyValueChangeProposal.class, DefaultPropertyValueChangeProposal.class);
        OntologyJavaMappingUtil.add("RetireProposal", RetireProposal.class, DefaultRetireProposal.class);
        OntologyJavaMappingUtil.add("Question", Question.class, DefaultQuestion.class);
        OntologyJavaMappingUtil.add("Review", Review.class, DefaultReview.class);
        OntologyJavaMappingUtil.add("ReviewRequest", ReviewRequest.class, DefaultReviewRequest.class);
        OntologyJavaMappingUtil.add("SeeAlso", SeeAlso.class, DefaultSeeAlso.class);
        OntologyJavaMappingUtil.add("SimpleProposal", SimpleProposal.class, DefaultSimpleProposal.class);
        OntologyJavaMappingUtil.add("SplitProposal", SplitProposal.class, DefaultSplitProposal.class);
        OntologyJavaMappingUtil.add("Status", Status.class, DefaultStatus.class);        
        OntologyJavaMappingUtil.add("Vote", Vote.class, DefaultVote.class);
        OntologyJavaMappingUtil.add("VotingProposal", VotingProposal.class, DefaultVotingProposal.class);
    }
}
