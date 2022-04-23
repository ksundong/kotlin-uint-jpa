package dev.idion.kotlinuint.uint;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import kotlin.UInt;
import org.jetbrains.annotations.Nullable;

@Entity
public class UintTest {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Convert(converter = UIntConverter.class)
  private UInt mau;

  protected UintTest() {
  }

  public UintTest(Long id, UInt mau) {
    this.id = id;
    this.mau = mau;
  }

  public UintTest(@Nullable UInt mau) {
    this(null, mau);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UInt getMau() {
    return mau;
  }

  public void setMau(UInt mau) {
    this.mau = mau;
  }
}
