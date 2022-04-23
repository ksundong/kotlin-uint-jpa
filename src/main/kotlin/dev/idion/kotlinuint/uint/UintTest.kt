package dev.idion.kotlinuint.uint

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY

@Entity
class UintTest(
    @Column
    @Convert(converter = UIntConverter::class)
    var mau: UInt? = null,

    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null,
)

@Converter
class UIntConverter : AttributeConverter<UInt?, Int?> {

    private val logger: Logger = LoggerFactory.getLogger(UIntConverter::class.java)

    override fun convertToDatabaseColumn(attribute: UInt?): Int? {
        logger.info("엔티티 값: {}", attribute)
        logger.info("DB에 저장될 값: {}", attribute?.toInt())
        return attribute?.toInt()
    }

    override fun convertToEntityAttribute(dbData: Int?): UInt? {
        logger.info("DB에 저장된 값: {}", dbData)
        logger.info("엔티티로 반환될 값: {}", dbData?.toUInt())
        return dbData?.toUInt()
    }
}
