package de.unijena.cheminf.npdatabasefiller.model;


import javax.persistence.*;

@Entity
@Table(name="fragment_and_fragment", indexes = {  @Index(name = "IDX1", columnList = "fid1"),@Index(name = "IDX2", columnList = "fid2") ,
        @Index(name = "IDX3", columnList = "fid1, fid2") } )
public class FragmentAndFragment {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer fandf_id;

    private Integer fid1;

    private Integer fid2;

    private Double joint_frequency;





    public Integer getFandf_id() {
        return fandf_id;
    }

    public void setFandf_id(Integer fandf_id) {
        this.fandf_id = fandf_id;
    }

    public Integer getFid1() {
        return fid1;
    }

    public void setFid1(Integer fid1) {
        this.fid1 = fid1;
    }

    public Integer getFid2() {
        return fid2;
    }

    public void setFid2(Integer fid2) {
        this.fid2 = fid2;
    }

    public Double getJoint_frequency() {
        return joint_frequency;
    }

    public void setJoint_frequency(Double joint_frequency) {
        this.joint_frequency = joint_frequency;
    }
}
