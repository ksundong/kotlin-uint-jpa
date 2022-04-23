package dev.idion.kotlinuint.uint1

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class UintTest(
    @Column
    var mau: UInt? = null,
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long
) {
}
