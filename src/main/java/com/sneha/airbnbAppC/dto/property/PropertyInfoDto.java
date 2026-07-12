package com.sneha.airbnbAppC.dto.property;

import com.sneha.airbnbAppC.dto.room.RoomResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfoDto {

    private PropertyResponseDto property;
    private List<RoomResponseDto> room;

}
