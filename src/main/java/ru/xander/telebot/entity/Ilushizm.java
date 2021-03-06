package ru.xander.telebot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.Instant;

/**
 * @author Alexander Shakhov
 */
@Entity
@Data
@NoArgsConstructor
@ToString
public class Ilushizm {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ilushizm_seq")
    @SequenceGenerator(name = "ilushizm_seq", sequenceName = "ilushizm_sequence", allocationSize = 1)
    private Long id;
    @Column(name = "txt", length = 2000)
    private String text;
    private String creator;
    private Instant created;
    private Boolean accepted;
}
