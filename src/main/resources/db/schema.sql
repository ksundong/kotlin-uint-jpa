CREATE TABLE `uint`.`uint_test`
(
    id  BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'id',
    mau INT UNSIGNED NULL COMMENT 'mau는 21억을 넘을 수 있을까',
    CONSTRAINT uint_test_pk
        PRIMARY KEY (id)
)
    COMMENT 'unsigned 타입이 존재하는 테이블';
