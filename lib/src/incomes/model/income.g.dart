// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'income.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Income _$IncomeFromJson(Map<String, dynamic> json) => Income(
      json['id'] as String,
      (json['version'] as num).toInt(),
      json['date'] as String,
      json['description'] as String,
      json['category'] as String,
      json['amount'] as String,
    );

Map<String, dynamic> _$IncomeToJson(Income instance) => <String, dynamic>{
      'id': instance.id,
      'version': instance.version,
      'date': instance.date,
      'description': instance.description,
      'category': instance.category,
      'amount': instance.amount,
    };
