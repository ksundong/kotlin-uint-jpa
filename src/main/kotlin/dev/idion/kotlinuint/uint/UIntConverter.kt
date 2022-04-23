package dev.idion.kotlinuint.uint

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class UIntConverter : AttributeConverter<UInt?, Long?> {

    private val logger: Logger = LoggerFactory.getLogger(UIntConverter::class.java)

    override fun convertToDatabaseColumn(attribute: UInt?): Long? {
        logger.info("엔티티 값: {}", attribute)
        logger.info("DB에 저장될 값: {}", attribute?.toLong())
        return attribute?.toLong()
    }

    override fun convertToEntityAttribute(dbData: Long?): UInt? {
        logger.info("DB에 저장된 값: {}", dbData)
        logger.info("엔티티로 반환될 값: {}", dbData?.toUInt())
        return dbData?.toUInt()
    }
}
